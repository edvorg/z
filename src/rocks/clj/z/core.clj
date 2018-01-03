(ns rocks.clj.z.core
  (:import [java.util.zip ZipOutputStream ZipEntry ZipInputStream])
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(defmacro ^:private with-new-entry [zip entry-name & body]
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
        (with-new-entry output path
          (-> (io/input-stream input)
              (io/copy output)))))))

(defn- zip-entries
  "Creates lazy seq of zip entries."
  [^ZipInputStream zip-input]
  (lazy-seq
    (if-let [entry (.getNextEntry zip-input)]
      (cons entry (zip-entries zip-input)))))

(defn do-zip
  "Reads zip archive from input and applies f to [ZipInputStream ZipEntry] for each entry.
  Input can be anything that's accepted by clojure.java.io/input-stream."
  [input f]
  (with-open [zip-input (ZipInputStream. (io/input-stream input))]
    (doseq [entry (zip-entries zip-input)]
      (f zip-input entry)
      (.closeEntry zip-input))))

(defn extract
  "Extracts input archive to output directory.
  Input can be anything that's accepted by clojure.java.io/input-stream.
  Output can be anything that is accepted by clojure.java.io/file."
  [input output]
  (do-zip
    input
    (fn [^ZipInputStream zip-input ^ZipEntry entry]
      (let [filename    (.getName entry)
            filename    (if (s/starts-with? filename "/")
                          (subs filename 1)
                          filename)
            output-file (io/file output filename)]
        (io/make-parents output-file)
        (io/copy zip-input output-file)))))

(defn reduce-zip
  "Reads zip archive from input and reduces all entries applying f to [ZipInputStream val ZipEntry].
  Result of f becomes new val. Input can be anything that's accepted by clojure.java.io/input-stream."
  [f val input]
  (with-open [zip-input (ZipInputStream. (io/input-stream input))]
    (->> (zip-entries zip-input)
         (reduce (fn [val ^ZipEntry entry]
                   (let [result (f zip-input val entry)]
                     (.closeEntry zip-input)
                     result))
                 val))))

(comment
  (compress "test.zip"
            :entries {"/var/raven/incoming-files/test.json" "/var/raven/incoming-files/test.json"})

  (compress "test.zip"
            :entries {"/var/raven/incoming-files/test.json" true})

  (compress "test.zip"
            :entries ["/var/raven/incoming-files/test.json"])

  ;; fully lazy mode, doesn't retain lazy seq
  (compress "z.zip"
            :entries-fn (fn []
                          (->> (io/file "/Users/edvorg/Projects/z")
                               file-seq
                               (filter #(.isFile %))
                               (map #(.getPath %)))))

  (extract "test.zip"
           "test")

  (->> "test.zip"
       (reduce-zip
         (fn [zip-input val entry]
           (conj val (.getName entry)))
         []))
  )
