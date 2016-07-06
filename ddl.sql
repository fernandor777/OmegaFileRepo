CREATE TABLE archivos.doc_file
(
  archivo_oid oid NOT NULL,
  nombre_archivo character varying(255) NOT NULL,
  enlazado boolean NOT NULL,
  instante_insert timestamp without time zone NOT NULL,
  content_type character varying(255),
  CONSTRAINT doc_file_pkey PRIMARY KEY (archivo_oid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE archivos.doc_file
  OWNER TO sisapp;
