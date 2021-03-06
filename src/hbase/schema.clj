(ns hbase.schema
	(:gen-class)
	(:import [org.apache.hadoop.hbase HTableDescriptor HColumnDescriptor]
	         [org.apache.hadoop.hbase.client HBaseAdmin]
	         [org.apache.hadoop.hbase.util Bytes]))

(defn create-table [table-name & arguments]
	(let [arguments (vec arguments) admin (HBaseAdmin. (peek arguments)) descriptor (HTableDescriptor. table-name) column-families (pop arguments)]
		(doseq  [column-family column-families]
			(.addFamily descriptor (HColumnDescriptor. column-family)))
		(try
			(.createTable admin descriptor)
			(catch Exception e 
				(println (str "create-table exception: " (.getMessage e)))))))

(defn drop-table [table-name config]
	(let [admin (HBaseAdmin. config) table-name-bytes (Bytes/toBytes table-name)]
		(.disableTable admin table-name-bytes)
		(.deleteTable admin table-name-bytes)))

