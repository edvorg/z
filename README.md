# z

A simple Clojure wrapper around java.util.zip

[![Clojars Project](https://img.shields.io/clojars/v/rocks.clj/z.svg)](https://clojars.org/rocks.clj/z)

## Usage

```clojure
(ns ...
  (:require [rocks.clj.z.core :refer [compress]]))

(compress "test.zip" :entries {"test.json" "test.json"})

(compress "test.zip" :entries {"test.json" true})

(compress "test.zip" :entries ["test.json"])

;; fully lazy mode, doesn't retain lazy seq
(compress "/Users/edvorg/Downloads/z.zip"
          :entries-fn (fn []
                        (->> (io/file "/Users/edvorg/Projects/z")
                             file-seq
                             (filter #(.isFile %))
                             (map #(.getPath %)))))
```

## License

Copyright © 2017 Eduard Knyshov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
