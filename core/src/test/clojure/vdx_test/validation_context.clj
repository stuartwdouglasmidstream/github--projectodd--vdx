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

(ns vdx-test.validation-context
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import (org.projectodd.vdx.core ValidationContext)
           (javax.xml.namespace QName)
           [org.projectodd.vdx.core.schema SchemaElement]))

(defn assert-position [line col pos]
  (is (= line (.line pos)))
  (is (= col (.col pos))))

(deftest attributesForElement-should-work
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
                                [(io/resource "schemas/handler-test.xsd")])]
    (is (= #{"attr1" "some-attr"} (.attributesForElement ctx [(SchemaElement. (QName. "urn:vdx:test" "foo"))
                                                              (SchemaElement. (QName. "urn:vdx:test" "bar"))])))))

(deftest searchForward
  (let [ctx (ValidationContext. (io/resource "search-test.xml")
                                [(io/resource "schemas/handler-test.xsd")])]
    (assert-position 3 2 (.searchForward ctx 0 0 #"foo "))
    (assert-position 3 2 (.searchForward ctx 1 1 #"foo "))
    (is (not (.searchForward ctx 3 1 #"foo ")))))

(deftest searchBackward
  (let [ctx (ValidationContext. (io/resource "search-test.xml")
                                [(io/resource "schemas/handler-test.xsd")])]
    (assert-position 3 33 (.searchBackward ctx 4 4 #"foo "))
    (is (not (.searchBackward ctx 0 0 #"foo ")))
    (is (not (.searchBackward ctx 4 4 #"asdfsafd")))))
