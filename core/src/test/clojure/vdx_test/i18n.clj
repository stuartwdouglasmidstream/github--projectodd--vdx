(ns vdx-test.i18n
  (:require [clojure.test :refer :all])
  (:import [org.projectodd.vdx.core I18N I18N$Key]))


(deftest all-keys-should-have-entries
  (doseq [k (I18N$Key/values)]
    (is (I18N/lookup k))))
