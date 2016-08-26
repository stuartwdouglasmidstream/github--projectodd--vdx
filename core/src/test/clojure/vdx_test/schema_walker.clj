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

