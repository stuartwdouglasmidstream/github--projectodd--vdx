(ns vdx-test.handlers
    (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
    (:import (org.projectodd.vdx.core.handlers
               DuplicateAttributeHandler
               DuplicateElementHandler
               InvalidAttributeValueHandler
               RequiredAttributeMissingHandler
               RequiredElementMissingHandler
               RequiredElementsMissingHandler
               UnexpectedAttributeHandler
               UnexpectedElementEndHandler
               UnexpectedElementHandler
               UnsupportedElementHandler)
      (org.projectodd.vdx.core ValidationContext ValidationError ErrorType SchemaElement I18N$Key)
      (javax.xml.stream Location)
      (javax.xml.namespace QName)
      (java.util List)))

(defn location [line col]
  (reify Location
    (getLineNumber [_] line)
    (getColumnNumber [_] col)))

(defn coerce-value [v]
  (cond
    (instance? SchemaElement v) (.name v)
    (instance? List v) (map coerce-value v)
    :default v))

(defn assert-message [msg template & values]
  (is (= template (.template msg)))
  (is (= values (map coerce-value (.rawValues msg)))))

(deftest test-UnexpectedAttributeHandler
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
                                (io/resource "schemas")
                                [(io/resource "schemas/handler-test.xsd")])]
    (testing "unmatchable attribute with no alternates"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           (QName. "biscuit")
                                           nil))]
        (is (= 6 (.line res)))
        (is (= 8 (.column res)))
        (assert-message (.message res)
                        I18N$Key/ATTRIBUTE_NOT_ALLOWED
                        "biscuit" "ham")
        (is (nil? (.extraMessage res)))))

    (testing "unmatchable attribute with schema alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "blahblahblah")
                                           nil))]
        (assert-message (.extraMessage res)
                        I18N$Key/ATTRIBUTES_ALLOWED_HERE
                        ["attr1" "some-attr"])))

    (testing "unmatchable attribute with provided alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "blahblahblah")
                                           #{"abc"}))]
        (assert-message (.extraMessage res)
                        I18N$Key/ATTRIBUTES_ALLOWED_HERE
                        ["abc"])))

    (testing "misspelled attribute with schema alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "attr2")
                                           nil))]
        (is (= 18 (.column res)))
        (assert-message (.extraMessage res)
                        I18N$Key/DID_YOU_MEAN
                        "attr1")))

    (testing "misspelled attribute with provided alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "attr2")
                                           #{"attrx"}))]
        (is (= 18 (.column res)))
        (assert-message (.extraMessage res)
                        I18N$Key/DID_YOU_MEAN "attrx")))

    (testing "matchable attribute"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "attr3")
                                           nil))]
        (is (= 28 (.column res)))
        (assert-message (.extraMessage res)
                        I18N$Key/ATTRIBUTE_IS_ALLOWED_ON
                        "attr3" [["foo"]])))))

(deftest test-UnexpectedElementHandler
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
                                (io/resource "schemas")
                                [(io/resource "schemas/handler-test.xsd")])]
    (testing "unmatchable element with no alternates"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           nil))]
        (is (= 6 (.line res)))
        (is (= 4 (.column res)))
        (assert-message (.message res)
                        I18N$Key/ELEMENT_NOT_ALLOWED
                        "ham")
        (is (nil? (.extraMessage res)))))

    (testing "unmatchable element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           #{"abcdefg"}))]
        (assert-message (.extraMessage res)
                        I18N$Key/ELEMENTS_ALLOWED_HERE
                        ["abcdefg"])))

    (testing "misspelled element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           #{"abc"}))]
        (assert-message (.extraMessage res)
                        I18N$Key/DID_YOU_MEAN
                        "abc")))

    (testing "matchable element"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 7 4)
                                           (QName. "urn:vdx:test" "sandwich")
                                           nil))]
        (assert-message (.extraMessage res)
                        I18N$Key/ELEMENT_IS_ALLOWED_ON
                        "sandwich" [["foo" "bar" "sandwiches"] ["omelet" "sandwiches"]])))))
