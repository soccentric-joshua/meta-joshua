_joshua_make_completion()
{
    local current_word previous_word targets
    current_word="${COMP_WORDS[COMP_CWORD]}"
    previous_word="${COMP_WORDS[COMP_CWORD-1]}"
    targets="build image rebuild-image sdk esdk shell swu flash deploy-images deploy-sdk deploy-esdk deploy-swu kas-image sdk-image esdk-image completion lint test validate check-board clean clean-build clean-docker clean-all help"

    if [[ "${previous_word}" == "TARGET" || "${previous_word}" == "TARGET="* ]]; then
        COMPREPLY=($(compgen -W "$(make -s help | sed -n 's/^Target platforms: //p')" -- "${current_word#TARGET=}"))
    else
        COMPREPLY=($(compgen -W "${targets}" -- "${current_word}"))
    fi
}

complete -F _joshua_make_completion make