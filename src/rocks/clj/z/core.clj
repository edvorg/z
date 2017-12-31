(ns rocks.clj.z.core
  (:import [java.util.zip ZipOutputStream ZipEntry])
  (:require [clojure.java.io :as io]))

(defmacro ^:private with-entry [zip entry-name & body]
  `(let [^ZipOutputStream zip# ~zip]
     (.putNextEntry zip# (ZipEntry. ~entry-name))
     ~@body
     (flush)
     (.closeEntry zip#)))

(defn compress
  "Writes a zip archive with entries to output.
   Output can be anything that is accepted by clojure.java.io/output-stream.
   Entries can be a collection of filenames or pairs [path -> input].
   Path is a path to entry inside archive.
   Input can be anything that is accepted by clojure.java.io/input-stream or true (input value defaults to path).
   Alternaively you can provide entries-fn function which returns lazy collection.
   It allows to archive big amount of data without retaining any lazy seqs in memory during the process."
  [output & {:keys [entries entries-fn]}]
  (with-open [output (ZipOutputStream. (io/output-stream output))]
    (doseq [entry (cond
                    entries    entries
                    entries-fn (entries-fn)
                    :default   (throw (ex-info "Either :entries or :entries-fn should be specified" {})))]
      (let [[path input] (cond
                           (string? entry) [entry entry]
                           (coll? entry)   entry
                           :default        (throw (ex-info "Entry should be either [path input] or string" {})))
            input        (if (= true input)
                           path
                           input)]
        (with-entry output path
          (-> (io/input-stream input)
              (io/copy output)))))))

(comment
  (compress "test.zip"
            :entries {"test.json" "test.json"})

  (compress "test.zip"
            :entries {"test.json" true})

  (compress "test.zip"
            :entries ["test.json"])

  ;; fully lazy mode, doesn't retain lazy seq
  (compress "/Users/edvorg/Downloads/z.zip"
            :entries-fn (fn []
                          (->> (io/file "/Users/edvorg/Projects/z")
                               file-seq
                               (filter #(.isFile %))
                               (map #(.getPath %)))))
  )
