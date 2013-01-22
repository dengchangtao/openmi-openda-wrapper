#! /bin/sh
# Script  : validate.sh
# Purpose : validate the xml-examples automatically with the xmllint script from the libxml2-package
xmllint --schema treeVector_v1.0.xsd --noout WRITTEN_BY_COSTA_example_1_very_simple.xml
xmllint --schema treeVector_v1.0.xsd --noout WRITTEN_BY_COSTA_example_2_fields_without_metadata.xml
xmllint --schema treeVector_v1.0.xsd --noout WRITTEN_BY_COSTA_example_3_fields_with_metadata.xml
xmllint --schema treeVector_v1.0.xsd --noout WRITTEN_BY_COSTA_example_4_timeseries.xml
