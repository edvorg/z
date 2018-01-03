# z

A simple Clojure wrapper around java.util.zip

[![Clojars Project](https://img.shields.io/clojars/v/rocks.clj/z.svg)](https://clojars.org/rocks.clj/z)

## Usage

```clojure
(ns ...
  (:require [rocks.clj.z.core :refer [compress]]))

(compress "test.zip"
          :entries {"test.json" "test.json"})

(compress "test.zip"
          :entries {"test.json" true})

(compress "test.zip"
          :entries ["test.json"])

;; fully lazy mode, doesn't retain lazy seq
(compress "z.zip"
          :entries-fn (fn []
                        (->> (io/file "/Users/edvorg/Projects/z")
                             file-seq
                             (filter #(.isFile %))
                             (map #(.getPath %)))))

;; extract test.zip into directory test
(extract "test.zip"
         "test")

;; perform a reduce operation on all entries
(->> "test.zip"
     (reduce-zip
       (fn [zip-input val entry]
         (conj val (.getName entry)))
       []))

;; find an entry and call a function on corresponding entry
(when (-> "investigation.zip"
          (seek-entry
            "/insert.edn"
            (fn [zip-input entry]
              (io/copy zip-input (io/file "insert.edn"))
              true)))
  (println "entry's found and unpacked"))
```

## License

Copyright © 2017 Eduard Knyshov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
