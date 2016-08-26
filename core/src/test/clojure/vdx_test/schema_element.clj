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

(ns vdx-test.schema-element
    (:require [clojure.test :refer :all])
    (:import [org.projectodd.vdx.core.schema SchemaElement]
      [javax.xml.namespace QName]))

(deftest equality
         (is (= (SchemaElement. (QName. "x")) (SchemaElement. (QName. "x"))))
         (is (not= (SchemaElement. (QName. "x")) nil))
         (is (= (SchemaElement. (QName. "x") (QName. "y")) (SchemaElement. (QName. "x") (QName. "y"))))
         (is (not= (SchemaElement. (QName. "x")) (SchemaElement. (QName. "y"))))
         (is (= (doto (SchemaElement. (QName. "x"))
                      (.addAttribute "y"))
                (doto (SchemaElement. (QName. "x"))
                      (.addAttribute "y")))))
