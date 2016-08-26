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

(ns vdx-test.tree
    (:require [clojure.test :refer :all])
    (:import [org.projectodd.vdx.core Tree]))

(deftest equality
         (is (= (Tree.) (Tree.)))
         (is (not= (Tree.) nil))
         (is (= (Tree. "x") (Tree. "x")))
         (is (not= (Tree. "x") (Tree. "y")))
         (is (= (doto (Tree. "x") (.addChild "y"))
                (doto (Tree. "x") (.addChild "y"))))
         (is (not= (doto (Tree. "x") (.addChild "z"))
                   (doto (Tree. "x") (.addChild "y")))))

