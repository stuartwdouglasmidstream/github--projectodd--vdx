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
           (org.projectodd.vdx.core ValidationContext ValidationError ErrorType SchemaElement)
           (javax.xml.stream Location)
           (javax.xml.namespace QName)))

(defn location [line col]
  (reify Location
    (getLineNumber [_] line)
    (getColumnNumber [_] col)))

(defn coerce-value [v]
  (cond
    (instance? SchemaElement v) (.name v)
    (instance? java.util.List v) (map coerce-value v)
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
          "'%s' isn't an allowed attribute for the '%s' element"
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
          "attributes allowed here are: %s"
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
          "attributes allowed here are: %s" ["abc"])))

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
          "Did you mean '%s'?" "attr1")))

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
         "Did you mean '%s'?" "attrx")))

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
          "'%s' is allowed on elements: %s\nDid you intend to put it on one of those elements?"
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
          "'%s' isn't an allowed element here", "ham")
        (is (nil? (.extraMessage res)))))

    (testing "unmatchable element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           #{"abcdefg"}))]
        (assert-message (.extraMessage res)
          "elements allowed here are: %s" ["abcdefg"])))

    (testing "misspelled element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           #{"abc"}))]
        (assert-message (.extraMessage res)
          "Did you mean '%s'?" "abc")))

    (testing "matchable element"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 7 4)
                                           (QName. "urn:vdx:test" "sandwich")
                                           nil))]
        (assert-message (.extraMessage res)
          "'%s' is allowed in elements: %s\nDid you intend to put it in one of those elements?"
          "sandwich" [["foo" "bar" "sandwiches"] ["omelet" "sandwiches"]])))))
