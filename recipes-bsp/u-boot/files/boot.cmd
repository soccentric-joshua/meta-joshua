fdt addr ${fdt_addr} && fdt get value bootargs /chosen bootargs
fatload mmc 0:1 ${kernel_addr_r} Image
if test ! -e mmc 0:1 uboot.env; then saveenv; fi
booti ${kernel_addr_r} - ${fdt_addr}
