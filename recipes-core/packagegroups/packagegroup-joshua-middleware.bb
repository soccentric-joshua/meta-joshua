SUMMARY = "Joshua middleware and development package group"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = " \
    grpc \
    protobuf \
    protobuf-lite \
    python3-grpcio \
    python3-protobuf \
"