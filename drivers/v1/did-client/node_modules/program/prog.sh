function prog() {
	export PROG_TARGET_CWD_FILE=/tmp/.prog_target_cwd.XXXXXX.tmp
	"$( dirname "${BASH_SOURCE[0]}" )"/prog.js $*

	if [ -f "$PROG_TARGET_CWD_FILE" ]
	then
		PROG_TARGET_CWD=$(cat "$PROG_TARGET_CWD_FILE")
		rm "$PROG_TARGET_CWD_FILE"
		cd "$PROG_TARGET_CWD"
	fi
}

function _prog_completion() {
	local args=()
	for x in "${COMP_WORDS[@]}";
	do
		args+=("a$x")
	done
	COMPREPLY=( $(prog complete ${args[@]}) );
}
complete -F _prog_completion prog

function _prog_open_completion() {
	local args=(aprog aopen)
	for x in "${COMP_WORDS[@]:1}";
	do
		args+=("a$x")
	done
	COMPREPLY=( $(prog complete ${args[@]}) );
}
complete -o nospace -F _prog_open_completion p

alias p="prog open"
