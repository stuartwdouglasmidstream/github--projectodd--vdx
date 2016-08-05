(ns vdx-test.validation-context
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import (org.projectodd.vdx.core ValidationContext)
           (javax.xml.namespace QName)))

(defn assert-position [line col pos]
  (is (= line (.line pos)))
  (is (= col (.col pos))))

(deftest attributesForElement-should-work
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
                                [(io/resource "schemas/handler-test.xsd")])]
    (is (= #{"attr1" "some-attr"} (.attributesForElement ctx (QName. "urn:vdx:test" "bar"))))))

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