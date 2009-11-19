#!/bin/bash
# Wrapper for clojure.jar invocations.

url=http://clojure.googlecode.com/files
clj_dir=$HOME/.clojure
zipfile=clojure_1.0.0.zip
jarfile=clojure-1.0.0.jar
clj_jar=$clj_dir/$jarfile
break_chars="(){}[],^%$#@\"\";:''|\\"
completions=$clj_dir/completions
java_opts="-Xmx256M -Dclojure.compile.path=."

type -p java > /dev/null || {
    echo "No java found, abort!" 1>&2
    exit 1
}

init=$clj_dir/cljrc.clj
if [ -f $init ] ; then
    source=$init
fi

# Download and install the clojure jarfile.
if [ ! -d $clj_dir ] ; then
    
    echo -n "Creating clojure directory $clj_dir..."
    mkdir -p $clj_dir || exit 1
    echo done.
fi    
if [ ! -f $clj_jar ] ; then
    if type -p wget > /dev/null ; then
        :
    else
        echo "wget not found" 1>&2
        echo "sudo apt-get install wget" 1>&2
        echo "to install wget" 1>&2
        exit 1
    fi

    echo -n "Downloading $zipfile..."
    trap "rm -f $clj_dir/$zipfile" 0 1 2
    wget -O $clj_dir/$zipfile $url/$zipfile || exit 1
    echo done.
    (
        cd $clj_dir && unzip $zipfile $jarfile && rm -f $zipfile || exit 1
    )
fi

# Set up keyword expansion file if not present.
if [ ! -f $completions ] ; then
    echo "Creating clojure keyword expansion file $completions" 1>&2
    java -cp $clj_jar clojure.main - <<EOF | sort -u > $completions
(let [completions
        (reduce concat (map (fn [p] (keys (ns-publics (find-ns p))))
                           '(clojure.core clojure.set clojure.xml clojure.zip)))]
    (println (apply str (interpose "\n" completions))))
EOF
fi

clj_cp=$clj_jar:${CLJ_CLASSPATH:-.}


if [ $# -eq 0 ]; then 
    rlwrap --remember -c -b $break_chars -f $completions \
        java $java_opts -cp $clj_cp clojure.lang.Repl $source
else
    java $java_opts -cp $clj_cp clojure.main "$@"
fi
