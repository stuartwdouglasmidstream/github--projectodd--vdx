(set-env! :dependencies '[[org.apache.ws.xmlschema/xmlschema-walker "2.2.2-SNAPSHOT"]
                          [commons-lang "2.6"]
                          [adzerk/boot-test "1.1.1" :scope "test"]]
          :resource-paths #{"src/main/java" "src/main/resources" "src/test/resources"}
          :source-paths #{"src/test/clojure"})
(require '[adzerk.boot-test :refer :all])

(deftask autotest
  "Watch for changes and rerun tests"
  []
  (comp (watch) (speak) (javac) (test)))


