(ns vdx-test.schema-walker
    (:require
        [clojure.test :refer :all]
        [clojure.java.io :as io])
    (:import [java.nio.file Paths]
      [org.projectodd.vdx.core.schema SchemaWalker SchemaElement]
      [org.projectodd.vdx.core Tree] [java.util Deque] [javax.xml.namespace QName]))

(defn schema-element [name & attrs]
      (let [el (SchemaElement. (QName. "urn:vdx:test" name))]
           (run! #(.addAttribute el %) attrs)
           el))

(deftest works
         (let [schema (io/resource "schemas/walker-test.xsd")
               walker (SchemaWalker. [schema])
               tree (Tree.)
               foo-tree (.addChild tree (schema-element "foo" "attr3"))
               _ (-> foo-tree
                     (.addChild (schema-element "bar" "attr1" "some-attr"))
                     (.addChild (schema-element "sandwiches"))
                     (.addChild (schema-element "sandwich")))
               biscuit-tree (.addChild foo-tree
                                       (schema-element "biscuit" "flake" "calories"))
               _ (-> biscuit-tree (.addChild (schema-element "filling")))
               _ (-> tree
                     (.addChild (schema-element "omelet"))
                     (.addChild (schema-element "sandwiches"))
                     (.addChild (schema-element "sandwich")))
               _ (.addChild tree (schema-element "filling"))
               output-tree (.walk walker)]
              (is (= tree output-tree))))

