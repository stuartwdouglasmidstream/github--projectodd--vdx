(set-env! :dependencies '[[adzerk/boot-test "1.1.1" :scope "test"]]
          :resource-paths #{"src/main/java" "src/main/resources" "src/test/resources"}
          :source-paths #{"src/test/clojure"})
(require '[adzerk.boot-test :refer :all])

(deftask autotest
  "Watch for changes and rerun tests"
  []
  (comp (watch) (speak) (javac) (test)))


