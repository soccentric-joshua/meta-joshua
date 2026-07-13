SUMMARY = "Joshua debugging, profiling, and test tools"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = " \
    gdbserver \
    perf \
    trace-cmd \
    valgrind \
    strace \
    ptest-runner \
"