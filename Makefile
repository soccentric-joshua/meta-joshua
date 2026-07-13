SHELL := /bin/bash

PROJECT_DIR := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))
BUILD_DIR ?= $(PROJECT_DIR)/build
DOWNLOADS_DIR ?= $(BUILD_DIR)/downloads
SSTATE_DIR ?= $(BUILD_DIR)/sstate-cache
DEPLOY_DIR ?= $(PROJECT_DIR)/deploy
KAS_CONTAINER ?= $(PROJECT_DIR)/scripts/kas-container
KAS_FILE ?= $(PROJECT_DIR)/kas/am62axx-evm.yml
IMAGE_TARGET ?= joshua-image
OTA_VERSION ?= $(shell date -u +%Y%m%d%H%M%S)
DOCKER_IMAGE ?= ghcr.io/siemens/kas/kas:4.5

export KAS_WORK_DIR := $(PROJECT_DIR)
export KAS_BUILD_DIR := $(BUILD_DIR)
export DL_DIR := $(DOWNLOADS_DIR)
export SSTATE_DIR := $(SSTATE_DIR)
export KAS_CONTAINER_IMAGE := $(DOCKER_IMAGE)

TARGETS := $(patsubst $(PROJECT_DIR)/kas/%.yml,%,$(wildcard $(PROJECT_DIR)/kas/*.yml))

.PHONY: help build image rebuild-image shell sdk esdk swu flash deploy-images deploy-sdk deploy-esdk deploy-swu kas-image sdk-image esdk-image completion lint test validate validate-metadata check-board clean clean-build clean-docker clean-all $(TARGETS)

help:
	@printf '%s\n' \
		'Usage: make <target> [TARGET=<platform>]' \
		'' \
		'Build:' \
		'  build image       Build the selected Yocto image' \
		'  rebuild-image     Clean image state and rebuild' \
		'  sdk esdk          Build the standard or extensible SDK' \
		'  swu               Build the SWUpdate bundle' \
		'  shell             Open an interactive KAS shell' \
		'' \
		'Validation and utilities:' \
		'  lint test validate check-board completion' \
		'  clean clean-build clean-docker clean-all' \
		'' \
		'Target platforms: $(TARGETS)'

KNOWN_GOALS := help build image rebuild-image shell sdk esdk swu flash deploy-images deploy-sdk deploy-esdk deploy-swu kas-image sdk-image esdk-image completion lint test validate check-board clean clean-build clean-docker clean-all
CLI_TARGET := $(firstword $(filter-out $(KNOWN_GOALS),$(MAKECMDGOALS)))
TARGET ?= $(or $(CLI_TARGET),am62axx-evm)
KAS_CONFIG = $(PROJECT_DIR)/kas/$(TARGET).yml

define check_target
	@test -f "$(KAS_CONFIG)" || { echo "Unknown target '$(TARGET)'. Available: $(TARGETS)" >&2; exit 2; }
endef

define kas
	$(call check_target)
	"$(KAS_CONTAINER)" --repo-rw $1 "$(KAS_CONFIG)"
endef

build image:
	$(call kas,build)
	$(MAKE) --no-print-directory TARGET=$(TARGET) deploy-images

$(foreach platform,$(TARGETS),$(eval $(platform): TARGET=$(platform)))
$(foreach platform,$(TARGETS),$(eval $(platform): image))

rebuild-image:
	$(call kas,cleansstate)
	$(MAKE) TARGET=$(TARGET) build

shell:
	$(call kas,shell)

sdk:
	$(call kas,build --target $(IMAGE_TARGET)-do-populate_sdk)
	@mkdir -p "$(DEPLOY_DIR)/sdk/$(TARGET)/$(OTA_VERSION)"
	@find "$(BUILD_DIR)/tmp/deploy/sdk" -maxdepth 1 -type f -exec cp -f {} "$(DEPLOY_DIR)/sdk/$(TARGET)/$(OTA_VERSION)/" \; 2>/dev/null || true

esdk:
	$(call kas,build --target $(IMAGE_TARGET)-do_populate_sdk_ext)
	@mkdir -p "$(DEPLOY_DIR)/esdk/$(TARGET)/$(OTA_VERSION)"
	@find "$(BUILD_DIR)/tmp/deploy/sdk" -maxdepth 1 -type f -exec cp -f {} "$(DEPLOY_DIR)/esdk/$(TARGET)/$(OTA_VERSION)/" \; 2>/dev/null || true

swu:
	@test "$(TARGET)" = "rpi5" || { echo 'The current SWU format is supported only by TARGET=rpi5.' >&2; exit 2; }
	$(call kas,build)
	@set -e; \
	command -v cpio >/dev/null || { echo 'cpio is required to package an SWU.' >&2; exit 1; }; \
	command -v gzip >/dev/null || { echo 'gzip is required to package an SWU.' >&2; exit 1; }; \
	deploy="$(BUILD_DIR)/tmp/deploy/images/$(shell awk '/^machine:/ {print $$2; exit}' "$(KAS_CONFIG)")"; \
	rootfs="$$(find "$$deploy" -maxdepth 1 -type f -name '*.ext4' -printf '%T@ %p\n' 2>/dev/null | sort -nr | head -n1 | cut -d' ' -f2-)"; \
	[ -n "$$rootfs" ] || { echo "No ext4 root filesystem found in $$deploy." >&2; exit 1; }; \
	stage="$$(mktemp -d)"; trap 'rm -rf "$$stage"' EXIT; \
	gzip -c "$$rootfs" > "$$stage/rootfs.ext4.gz"; \
	for artifact in kernel_2712.img bcm2712-rpi-5-b.dtb; do \
		found="$$(find "$$deploy" -maxdepth 1 -type f -name "$$artifact" -print -quit)"; \
		if [ -n "$$found" ]; then cp -f "$$found" "$$stage/$$artifact"; fi; \
	done; \
	cp -f swupdate/pre-install.sh swupdate/post-install.sh "$$stage/"; \
	sed "s/@@VERSION@@/$(OTA_VERSION)/g" swupdate/sw-description > "$$stage/sw-description"; \
	if [ "$(SECURE_MODE)" = "1" ]; then \
		[ -n "$(SWUPDATE_PRIVATE_KEY)" ] && [ -f "$(SWUPDATE_PRIVATE_KEY)" ] || { echo 'SECURE_MODE=1 requires SWUPDATE_PRIVATE_KEY.' >&2; exit 2; }; \
		command -v swupdate-signer >/dev/null || { echo 'swupdate-signer is required in secure mode.' >&2; exit 1; }; \
		swupdate-signer -k "$(SWUPDATE_PRIVATE_KEY)" -s "$$stage/sw-description"; \
	fi; \
	mkdir -p "$(DEPLOY_DIR)/swu/$(TARGET)/$(OTA_VERSION)"; \
	( cd "$$stage" && find . -type f -print | sort | cpio --quiet -o -H crc ) > "$(DEPLOY_DIR)/swu/$(TARGET)/$(OTA_VERSION)/joshua-$(TARGET)-$(OTA_VERSION).swu"

flash:
	@set -e; \
	image="$$(find "$(BUILD_DIR)/tmp/deploy/images" -type f -name '*.wic.bz2' -printf '%T@ %p\n' 2>/dev/null | sort -nr | head -n1 | cut -d' ' -f2-)"; \
	[ -n "$$image" ] || { echo 'No WIC image found; run make image first.' >&2; exit 1; }; \
	command -v bmaptool >/dev/null || { echo 'bmaptool is required for flashing.' >&2; exit 1; }; \
	[ -n "$(DEVICE)" ] || { echo 'Set DEVICE=/dev/... to flash.' >&2; exit 2; }; \
	case "$(DEVICE)" in /dev/*) ;; *) echo 'DEVICE must be a block device path.' >&2; exit 2;; esac; \
	[ -b "$(DEVICE)" ] || { echo "$(DEVICE) is not a block device." >&2; exit 2; }; \
	bmaptool copy --nobmap "$$image" "$(DEVICE)"

deploy-images:
	@mkdir -p "$(DEPLOY_DIR)/images/$(TARGET)/$(OTA_VERSION)"
	@find "$(BUILD_DIR)/tmp/deploy/images" -type f \( -name '*.wic*' -o -name '*.bmap' -o -name '*.ext4' \) -exec cp -f {} "$(DEPLOY_DIR)/images/$(TARGET)/$(OTA_VERSION)/" \; 2>/dev/null || true
deploy-sdk:
	@mkdir -p "$(DEPLOY_DIR)/sdk/$(TARGET)/$(OTA_VERSION)"; find "$(BUILD_DIR)/tmp/deploy/sdk" -maxdepth 1 -type f -exec cp -f {} "$(DEPLOY_DIR)/sdk/$(TARGET)/$(OTA_VERSION)/" \; 2>/dev/null || true
deploy-esdk:
	@mkdir -p "$(DEPLOY_DIR)/esdk/$(TARGET)/$(OTA_VERSION)"; find "$(BUILD_DIR)/tmp/deploy/sdk" -maxdepth 1 -type f -exec cp -f {} "$(DEPLOY_DIR)/esdk/$(TARGET)/$(OTA_VERSION)/" \; 2>/dev/null || true
deploy-swu:
	@mkdir -p "$(DEPLOY_DIR)/swu/$(TARGET)/$(OTA_VERSION)"; find "$(BUILD_DIR)/tmp/deploy/images" -maxdepth 2 -type f -name '*.swu' -exec cp -f {} "$(DEPLOY_DIR)/swu/$(TARGET)/$(OTA_VERSION)/" \; 2>/dev/null || true

kas-image:
	@command -v docker >/dev/null || { echo 'Docker is required.' >&2; exit 1; }
	docker build --build-arg KAS_IMAGE=$(DOCKER_IMAGE) -t $(DOCKER_IMAGE) docker/kas
sdk-image: sdk
	@set -e; context="$$(mktemp -d)"; trap 'rm -rf "$$context"' EXIT; \
		cp -f docker/sdk/Dockerfile "$$context/Dockerfile"; \
		find "$(BUILD_DIR)/tmp/deploy/sdk" -maxdepth 1 -type f -exec cp -f {} "$$context/" \; 2>/dev/null; \
		docker build -t "$(DOCKER_IMAGE)-sdk" "$$context"
esdk-image: esdk
	@set -e; context="$$(mktemp -d)"; trap 'rm -rf "$$context"' EXIT; \
		cp -f docker/esdk/Dockerfile "$$context/Dockerfile"; \
		find "$(BUILD_DIR)/tmp/deploy/sdk" -maxdepth 1 -type f -exec cp -f {} "$$context/" \; 2>/dev/null; \
		docker build -t "$(DOCKER_IMAGE)-esdk" "$$context"

completion:
	@install -Dm644 scripts/joshua-completion.bash "$(HOME)/.local/share/bash-completion/completions/joshua"

lint:
	@bash -n scripts/kas-container
	@$(MAKE) -n help >/dev/null

test:
	@command -v docker >/dev/null || command -v podman >/dev/null || { echo 'Docker or Podman is required.' >&2; exit 1; }
	@command -v curl >/dev/null || { echo 'curl is required.' >&2; exit 1; }
	@$(MAKE) check-board
	@$(MAKE) validate

validate:
	@$(MAKE) check-board
	@for file in kas/*.yml; do python3 -c 'import sys; import yaml; yaml.safe_load(open(sys.argv[1]))' "$$file" || exit $$?; done
	@$(MAKE) --no-print-directory validate-metadata

validate-metadata:
	@set -e; for file in kas/*.yml; do \
		SSTATE_DIR="$(SSTATE_DIR)" DL_DIR="$(DOWNLOADS_DIR)" KAS_WORK_DIR="$(PROJECT_DIR)" KAS_BUILD_DIR="$(BUILD_DIR)" \
		"$(KAS_CONTAINER)" --repo-rw shell -c 'bitbake -p joshua-image' "$$file"; \
	done

check-board:
	@set -e; for file in kas/*.yml; do machine="$$(awk '/^machine:/ {print $$2; exit}' "$$file")"; [ -n "$$machine" ] || { echo "Missing machine in $$file" >&2; exit 1; }; \
		test -f "$(PROJECT_DIR)/conf/layer.conf" || { echo 'Project layer metadata is missing.' >&2; exit 1; }; \
		case "$$machine" in raspberrypi5) grep -q 'meta-raspberrypi:' "$$file" || { echo "$$file must declare meta-raspberrypi" >&2; exit 1; };; esac; \
		echo "$$file: $$machine"; done

clean:
	"$(KAS_CONTAINER)" clean "$(KAS_CONFIG)"
clean-build:
	rm -rf "$(BUILD_DIR)/tmp" "$(BUILD_DIR)/cache"
clean-docker:
	@command -v docker >/dev/null && docker image rm -f "$(DOCKER_IMAGE)" "$(DOCKER_IMAGE)-sdk" "$(DOCKER_IMAGE)-esdk" || true
clean-all: clean-build
	rm -rf "$(DOWNLOADS_DIR)" "$(SSTATE_DIR)" "$(DEPLOY_DIR)"