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

(ns vdx-test.i18n
  (:require [clojure.test :refer :all])
  (:import [org.projectodd.vdx.core I18N I18N$Key]
           java.util.Locale))

(deftest all-keys-should-have-entries
  (doseq [k (I18N$Key/values)]
    (is (I18N/lookup k))))

(deftest can-load-other-locale
  (I18N/reset)
  (I18N/setLocale Locale/GERMANY)
  (is (= "Meinten Sie \"%s\"?" (I18N/lookup I18N$Key/DID_YOU_MEAN))))

(deftest uses-default-messages-when-locale-not-found
  (I18N/reset)
  (I18N/setLocale Locale/ITALY)
  (is (= "Did you mean '%s'?" (I18N/lookup I18N$Key/DID_YOU_MEAN))))
