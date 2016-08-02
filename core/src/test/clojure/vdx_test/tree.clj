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

