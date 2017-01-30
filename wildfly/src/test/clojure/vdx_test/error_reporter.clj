(ns vdx-test.error-reporter
  (:require [clojure.test :refer :all])
  (:import org.projectodd.vdx.wildfly.ErrorReporter))

(deftest message-code-stripping
  (are [given]
    (= "Missing required attribute(s): name" (.get (ErrorReporter/stripMessageCode given)))

    "ParseError at [row,col]:[364,9]\nMessage: WFLYCTL0133: Missing required attribute(s): name"
    "ParseError at [row,col]:[364,9]\nMessage: \"WFLYCTL0133: Missing required attribute(s): name\""
    "WFLYCTL0133: Missing required attribute(s): name"
    "\"WFLYCTL0133: Missing required attribute(s): name\""))
