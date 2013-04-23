(ns hbase.table
	(:gen-class)
	(:refer-clojure :exclude [get])
	(:require [hbase.config])
	(:import [org.apache.hadoop.hbase.util Bytes]
	         [org.apache.hadoop.hbase.client Put Get HTable]))

(defn connect [name config]
	(if (not= config nil) 
		(HTable. config name)
		(HTable. (hbase.config/create) name)))

(defn create-put [rowkey] 
	(Put. (Bytes/toBytes rowkey)))

(defn create-get [rowkey] 
	(Get. (Bytes/toBytes rowkey)))

(defmulti put (fn[& arglist] (count arglist)))

(defmethod put 4 [self rowkey column-family data]
	(let [p (create-put rowkey)]
		(doseq [[column-name value] data]
			(.add p
				(Bytes/toBytes (str column-family))
				(Bytes/toBytes (str column-name))
				(Bytes/toBytes (str value))))
		(.put self p)))

(defmethod put 5 [self rowkey column-family column-name value]
    (let [p (create-put rowkey)]
		(.add p
			(Bytes/toBytes (str column-family))
			(Bytes/toBytes (str column-name))
			(Bytes/toBytes (str value)))
		(.put self p)))

(defn get-to-map [get]
	(let [key-values (-> get .raw seq) return (ref {})]
		(doseq [key-value key-values]
			(dosync
				(alter return assoc-in [
					(Bytes/toString (.getFamily key-value))
					(Bytes/toString (.getQualifier key-value)) 
					(.getTimestamp key-value)]
						(Bytes/toString (.getValue key-value)))))
		@return))

(defmulti get (fn[& arglist] (count arglist)))

(defmethod get 2 [self rowkey]
	(let [get-operation (create-get rowkey)]
		(get-to-map
			(.get self get-operation))))

(defmethod get 3 [self rowkey column-family]
	(let [get-operation (create-get rowkey)]
		(.addFamily get-operation (Bytes/toBytes column-family))
		(get-to-map
			(.get self get-operation))))

(defmethod get 4 [self rowkey column-family column-name]
	(let [get-operation (create-get rowkey)]
		(.addColumn get-operation
			(Bytes/toBytes column-family)
			(Bytes/toBytes column-name))
		(get-to-map
			(.get self get-operation))))


