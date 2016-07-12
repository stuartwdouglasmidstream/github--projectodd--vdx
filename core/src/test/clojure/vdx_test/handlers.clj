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
           (org.projectodd.vdx.core ValidationContext ValidationError ErrorType)
           (javax.xml.stream Location)
           (javax.xml.namespace QName)))

(defn location [line col]
  (reify Location
    (getLineNumber [_] line)
    (getColumnNumber [_] col)))

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
        (is (= "'biscuit' isn't an allowed attribute for the 'ham' element" (.message res)))
        (is (nil? (.extraMessage res)))))

    (testing "unmatchable attribute with schema alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "blahblahblah")
                                           nil))]
        (is (= "attributes allowed here are: attr1" (.extraMessage res)))))

    (testing "unmatchable attribute with provided alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "blahblahblah")
                                           #{"abc"}))]
        (is (= "attributes allowed here are: abc" (.extraMessage res)))))

    (testing "misspelled attribute with schema alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "attr2")
                                           nil))]
        (is (= 18 (.column res)))
        (is (= "Did you mean 'attr1'?" (.extraMessage res)))))

    (testing "misspelled attribute with provided alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "attr2")
                                           #{"attrx"}))]
        (is (= 18 (.column res)))
        (is (= "Did you mean 'attrx'?" (.extraMessage res)))))

    (testing "matchable attribute"
      (let [res (.handle (UnexpectedAttributeHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                                           (location 4 4)
                                           (QName. "urn:vdx:test" "bar")
                                           (QName. "attr3")
                                           nil))]
        (is (= 28 (.column res)))
        (is (= "'attr3' is allowed on elements: foo\nDid you intend to put it on one of those elements?"
               (.extraMessage res)))))
    ))

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
        (is (= "'ham' isn't an allowed element here" (.message res)))
        (is (nil? (.extraMessage res)))))

    (testing "unmatchable element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           #{"abcdefg"}))]
        (is (= "elements allowed here are: abcdefg" (.extraMessage res)))))

    (testing "misspelled element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 6 4)
                                           (QName. "urn:vdx:test" "ham")
                                           #{"abc"}))]
        (is (= "Did you mean 'abc'?" (.extraMessage res)))))

    (testing "matchable element"
      (let [res (.handle (UnexpectedElementHandler.)
                         ctx
                         (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                                           (location 7 4)
                                           (QName. "urn:vdx:test" "sandwich")
                                           nil))]
        (is (= "'sandwich' is allowed in elements: foo > bar > sandwiches, omelet > sandwiches\nDid you intend to put it in one of those elements?"
               (.extraMessage res)))))
    ))
