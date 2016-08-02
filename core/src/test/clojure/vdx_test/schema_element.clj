(ns vdx-test.schema-element
    (:require [clojure.test :refer :all])
    (:import [org.projectodd.vdx.core.schema SchemaElement]
      [javax.xml.namespace QName]))

(deftest equality
         (is (= (SchemaElement. (QName. "x")) (SchemaElement. (QName. "x"))))
         (is (not= (SchemaElement. (QName. "x")) nil))
         (is (= (SchemaElement. (QName. "x") (QName. "y")) (SchemaElement. (QName. "x") (QName. "y"))))
         (is (not= (SchemaElement. (QName. "x")) (SchemaElement. (QName. "y"))))
         (is (= (doto (SchemaElement. (QName. "x"))
                      (.addAttribute "y"))
                (doto (SchemaElement. (QName. "x"))
                      (.addAttribute "y")))))
