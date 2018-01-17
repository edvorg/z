# z

A simple Clojure wrapper around java.util.zip

[![Clojars Project](https://img.shields.io/clojars/v/rocks.clj/z.svg)](https://clojars.org/rocks.clj/z)

## Usage

```clojure
(ns ...
  (:require [rocks.clj.z.core :refer [compress]]))

;; following three operations do the same thing

(compress "test.zip" :entries {"test.json" "test.json"})

(compress "test.zip" :entries {"test.json" true}) ;; true means that input file should default to entry name

(compress "test.zip" :entries ["test.json"])

;; real world example of compressing a file/directory
(->> (io/file "/Users/edvorg/Projects/z")
     file-seq
     (filter #(.isFile %))
     (map #(.getPath %))
     (compress "z.zip" :entries))

(compress "test.zip" :entries ["test.json"
                               "test.json" ;; repeated entries are dropped from compress process
                               "z.zip"])

;; extract archive into directory
(extract "investigation.zip" "investigation")

;; perform a reduce operation on all entries
(->> "investigation.zip"
     (reduce-zip
       (fn [zip-input val entry]
         (conj val (.getName entry)))
       []))

;; find an entry and call a function on corresponding entry
(-> "investigation.zip"
    (seek-entry
      "/insert.edn"
      (fn [zip-input entry]
        (io/copy zip-input (io/file "insert.edn"))
        true))) ;; this value is returned from seek-entry
```

## License

Copyright © 2017-2018 Eduard Knyshov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
