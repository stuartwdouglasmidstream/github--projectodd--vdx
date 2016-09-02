;; Copyright 2016 Red Hat, Inc, and individual contributors.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns vdx-test.schema-walker
    (:require
        [clojure.test :refer :all]
        [clojure.java.io :as io])
    (:import
      [org.projectodd.vdx.core.schema SchemaWalker SchemaElement]
      [org.projectodd.vdx.core Tree]
      [javax.xml.namespace QName]))

(defn attr-str [attrs]
  (reduce (fn [acc attr] (format "%s %s" acc attr)) "" attrs))

(defn print-as-xml [tree prefix]
  (let [element (.value tree)]
    (when-not (.isRoot tree)
      (println (format "%s<%s%s%s>"
                       prefix
                       (.name element)
                       (attr-str (.attributes element))
                       (if (.isEmpty (.children tree))
                         "/"
                         ""))))
    (run! #(print-as-xml % (str "  " prefix))
          (.children tree))
    (when-not (or (.isRoot tree)
                  (.isEmpty (.children tree)))
      (println (format "%s</%s>"
                       prefix
                       (.name element))))))

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
              (.addChild (schema-element "sandwich"))
              (.addChild (schema-element "bacon"))
              (.addChild (schema-element "crisp")))
        biscuit-tree (.addChild foo-tree
                                (schema-element "biscuit" "flake" "calories"))
        gravy-tree (-> biscuit-tree
                       (.addChild (schema-element "gravy")))
        _ (-> gravy-tree
              (.addChild (schema-element "thickness")))
        _ (-> biscuit-tree
              (.addChild (schema-element "sconiness")))
        _ (-> biscuit-tree
              (.addChild (schema-element "filling")))
        _ (-> tree
              (.addChild (schema-element "omelet"))
              (.addChild (schema-element "sandwiches"))
              (.addChild (schema-element "sandwich"))
              (.addChild (schema-element "bacon"))
              (.addChild (schema-element "crisp")))
        _ (.addChild tree (schema-element "filling"))
        output-tree (.walk walker)]

    (comment
      (println "actual:")
      (print-as-xml output-tree "")
      (println)
      (println "expected:")
      (print-as-xml tree ""))

    (comment
      (println "TYPES")
      (doseq [[k v] (.types walker)]
        (println k)
        (print-as-xml (.elements v) "")))

    (is (= tree output-tree))))

