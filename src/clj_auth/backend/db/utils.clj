(ns clj-auth.backend.db.utils
  (:require
   [honey.sql :as sql]
   [next.jdbc.result-set :as rs]
   [next.jdbc :as jdbc]))

(def default-opts
  {:return-keys true
   :builder-fn rs/as-unqualified-kebab-maps})

(defn format-sql
  "Format HoneySQL query with default options"
  [query]
  (sql/format query {:quoted true}))

(defn execute-one!
  "Execute a query and return a single result"
  [datasource query]
  (jdbc/execute-one! datasource (format-sql query) default-opts))

(defn execute!
  "Execute a query and return all results"
  [datasource query]
  (jdbc/execute! datasource (format-sql query) default-opts))

(defn insert!
  "Insert a record and return the inserted row"
  [datasource table row]
  (execute-one! 
   datasource
   {:insert-into table
    :values [row]
    :returning :*}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn update!
  "Update records and return updated rows"
  [datasource table set-map where-clause]
  (execute!
   datasource
   {:update table
    :set set-map
    :where where-clause
    :returning :*}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn delete!
  "Delete records and return deleted rows"
  [datasource table where-clause]
  (execute!
   datasource
   {:delete-from table
    :where where-clause
    :returning :*}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn query
  "Execute a SELECT query"
  [datasource query-map]
  (execute! datasource query-map))

(defn query-one
  "Execute a SELECT query and return first result"
  [datasource query-map]
  (execute-one! datasource query-map))
