(ns vdx-test.validation-context
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import (org.projectodd.vdx.core ValidationContext ValidationError ErrorType)
           (javax.xml.stream Location)
           (javax.xml.namespace QName)))

(deftest attributesForElement-should-work
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
                                (io/resource "schemas")
                                [(io/resource "schemas/handler-test.xsd")])]
    (is (= #{"attr1" "some-attr"} (.attributesForElement ctx (QName. "urn:vdx:test" "bar"))))))