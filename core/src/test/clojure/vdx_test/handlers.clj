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
             UnexpectedElementHandler
             UnsupportedElementHandler
             UnknownErrorHandler)
           (org.projectodd.vdx.core ValidationContext ValidationError ErrorType I18N$Key)
           (org.projectodd.vdx.core.schema SchemaElement)
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
  (is (= (or values []) (map coerce-value (.rawValues msg)))))

(deftest test-DuplicateElementHandler
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
              [(io/resource "schemas/handler-test.xsd")])]
    (testing "with an attribute"
      (let [res (.handle (DuplicateElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/DUPLICATE_ELEMENT
                        ""
                        (location 7 4))
                    (.element (QName. "urn:vdx:test" "bar"))
                    (.attribute (QName. "attr1"))
                    (.attributeValue "a")))]
        (assert-message (first (.messages res))
          I18N$Key/ELEMENT_WITH_ATTRIBUTE_DUPLICATED "bar" "attr1" "a")
        (assert-message (first (.extraMessages res))
          I18N$Key/ELEMENT_WITH_ATTRIBUTE_DUPLICATED_FIRST_OCCURRENCE "bar" "attr1")
        (assert-message (first (.messages (first (.extraResults res))))
          I18N$Key/BLANK)))
    (testing "without an attribute"
      (let [res (.handle (DuplicateElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/DUPLICATE_ELEMENT
                        ""
                        (location 7 4))
                    (.element (QName. "urn:vdx:test" "bar"))))]
        (assert-message (first (.messages res))
          I18N$Key/ELEMENT_DUPLICATED "bar")
        (assert-message (first (.extraMessages res))
          I18N$Key/ELEMENT_DUPLICATED_FIRST_OCCURRENCE "bar")
        (assert-message (first (.messages (first (.extraResults res))))
          I18N$Key/BLANK)))))

(deftest test-UnexpectedAttributeHandler
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
              [(io/resource "schemas/handler-test.xsd")])]
    (testing "unmatchable attribute with no alternates"
      (let [res (.handle (UnexpectedAttributeHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                        ""
                        (location 6 4))
                    (.element (QName. "urn:vdx:test" "ham"))
                    (.attribute (QName. "biscuit"))))]
        (is (= 6 (.line res)))
        (is (= 8 (.column res)))
        (assert-message (first (.messages res))
          I18N$Key/ATTRIBUTE_NOT_ALLOWED
          "biscuit" "ham")
        (assert-message (first (.extraMessages res))
          I18N$Key/ELEMENT_HAS_NO_ATTRIBUTES
          "ham")))

    (testing "unmatchable attribute with schema alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                        ""
                        (location 4 4))
                    (.element (QName. "urn:vdx:test" "bar"))
                    (.attribute (QName. "blahblahblah"))))]
        (assert-message (second (.messages res))
          I18N$Key/ATTRIBUTES_ALLOWED_HERE
          ["attr1" "some-attr"])))

    (testing "unmatchable attribute with provided alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                        ""
                        (location 4 4))
                    (.element (QName. "urn:vdx:test" "bar"))
                    (.attribute (QName. "blahblahblah"))
                    (.alternatives #{"abc"})))]
        (assert-message (second (.messages res))
          I18N$Key/ATTRIBUTES_ALLOWED_HERE
          ["abc"])))

    (testing "misspelled attribute with schema alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                        ""
                        (location 4 4))
                    (.element (QName. "urn:vdx:test" "bar"))
                    (.attribute (QName. "attr2"))))]
        (is (= 18 (.column res)))
        (assert-message (nth (.messages res) 2)
          I18N$Key/DID_YOU_MEAN
          "attr1")))

    (testing "misspelled attribute with provided alternatives"
      (let [res (.handle (UnexpectedAttributeHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                        ""
                        (location 4 4))
                    (.element (QName. "urn:vdx:test" "bar"))
                    (.attribute (QName. "attr2"))
                    (.alternatives #{"attrx"})))]
        (is (= 18 (.column res)))
        (assert-message (nth (.messages res) 2)
          I18N$Key/DID_YOU_MEAN "attrx")))

    (testing "matchable attribute"
      (let [res (.handle (UnexpectedAttributeHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ATTRIBUTE
                        ""
                        (location 4 4))
                    (.element (QName. "urn:vdx:test" "bar"))
                    (.attribute (QName. "attr3"))))]
        (is (= 28 (.column res)))
        (assert-message (first (.extraMessages res))
          I18N$Key/ATTRIBUTE_IS_ALLOWED_ON
          "attr3" [["foo"]])))))

(deftest test-UnexpectedElementHandler
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
              [(io/resource "schemas/handler-test.xsd")])]
    (testing "it's really a duplicate"
      (let [res (.handle (UnexpectedElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                        ""
                        (location 7 4))
                    (.element (QName. "urn:vdx:test" "bar"))))]
        (assert-message (first (.messages res))
          I18N$Key/ELEMENT_DUPLICATED "bar")
        (assert-message (first (.extraMessages res))
          I18N$Key/ELEMENT_DUPLICATED_FIRST_OCCURRENCE "bar")
        (assert-message (first (.messages (first (.extraResults res))))
          I18N$Key/BLANK)))
    (testing "unmatchable element with no alternates"
      (let [res (.handle (UnexpectedElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                        ""
                        (location 6 4))
                    (.element (QName. "urn:vdx:test" "ham"))))]
        (is (= 6 (.line res)))
        (is (= 4 (.column res)))
        (assert-message (first (.messages res))
          I18N$Key/ELEMENT_NOT_ALLOWED
          "ham")
        (is (empty? (.extraMessages res)))))

    (testing "unmatchable element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                        ""
                        (location 6 4))
                    (.element (QName. "urn:vdx:test" "ham"))
                    (.alternatives #{"abcdefg"})))]
        (assert-message (first (.extraMessages res))
          I18N$Key/ELEMENTS_ALLOWED_HERE
          ["abcdefg"])))

    (testing "misspelled element with provided alternatives"
      (let [res (.handle (UnexpectedElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                        ""
                        (location 6 4))
                    (.element (QName. "urn:vdx:test" "ham"))
                    (.alternatives #{"ahm"})))]
        (assert-message (first (.extraMessages res))
          I18N$Key/DID_YOU_MEAN
          "ahm")))

    (testing "matchable element"
      (let [res (.handle (UnexpectedElementHandler.)
                  ctx
                  (-> (ValidationError. ErrorType/UNEXPECTED_ELEMENT
                        ""
                        (location 7 4))
                    (.element (QName. "urn:vdx:test" "sandwich"))))]
        (assert-message (first (.extraMessages res))
          I18N$Key/ELEMENT_IS_ALLOWED_ON
          "sandwich" [["foo" "bar" "sandwiches"] ["omelet" "sandwiches"]])))))

(deftest test-UnknownErrorHandler
  (let [ctx (ValidationContext. (io/resource "handler-test.xml")
              [(io/resource "schemas/handler-test.xsd")])]
    (let [res (.handle (UnknownErrorHandler.)
                ctx
                (-> (ValidationError. ErrorType/UNKNOWN_ERROR
                      "foo"
                      (location 1 1))
                  (.fallbackMessage "bar")))]
      (assert-message (first (.messages res))
        I18N$Key/PASSTHRU "bar"))))

