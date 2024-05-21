#!/bin/bash

PADDING_LENGTH=2
TEMPLATE_FILE_PREFIX="template"
SCHEMA_SUFFIX="_SCHEMA.json"
REALTIME_SUFFIX="_REALTIME.json"
OFFLINE_SUFFIX="_OFFLINE.json"

START_INDEX=$1
END_INDEX=$2
CONTROLLER_ADDRESS=$3

function usage() {
  echo "Usage: $0 <START_INDEX> <END_INDEX> [CONTROLLER_ADDRESS]"
  echo "  START_INDEX and END_INDEX must be non-negative integers up to ${PADDING_LENGTH} digits."
  echo "  Template files should be located in the execution directory."
  echo "  Example: $0 0 4 http://localhost:9000"
}

if [ "$#" -lt 2 ]; then
  usage
  exit 1
fi

function create_table_number_list() {
  if ! [[ "$START_INDEX" =~ ^[0-9]{1,"$PADDING_LENGTH"}$ ]] || ! [[ "$END_INDEX" =~ ^[0-9]{1,"$PADDING_LENGTH"}$ ]]; then
      echo "START_INDEX and END_INDEX must be non-negative integers up to ${PADDING_LENGTH} digits."
      exit 1
  fi

  # Create list with leading zeros
  NUM_LIST=()
  for (( i=10#$START_INDEX; i<=10#$END_INDEX; i++ )); do
      NUM=$(printf "%0${PADDING_LENGTH}d" "$i")
      NUM_LIST+=("$NUM")
  done

  # Check if list is not empty
  if [ ${#NUM_LIST[@]} -eq 0 ]; then
      echo "NO argument created with START_INDEX:${START_INDEX} and END_INDEX:${END_INDEX}"
      exit 1
  fi
}


function generate_config() {
  local JSON_SUFFIX=$1
  local TEMPLATE_FILE="${TEMPLATE_FILE_PREFIX}${JSON_SUFFIX}"
  if [ ! -f "${TEMPLATE_FILE}" ]; then
    echo "No ${TEMPLATE_FILE} file provided."
    return 0
  fi
  echo "  Generating ${JSON_SUFFIX} configuration using ${TEMPLATE_FILE}"

  for NUM in "${NUM_LIST[@]}"; do
    JSON_FILE="${NUM}${JSON_SUFFIX}"
    # Create JSON file using template file
    sed -e "s/\${NUM}/${NUM}/g" \
        "${TEMPLATE_FILE}" > "${JSON_FILE}"
    echo "  Configuration file ${JSON_FILE} generated successfully."
  done
}

function add_schema_to_pinot() {
  local JSON_SUFFIX=$1
  if [ -z "$CONTROLLER_ADDRESS" ]; then
    echo "No Controller address provided. Skipping table creation."
    return 0
  fi
  echo "  Adding schema to ${CONTROLLER_ADDRESS}"

  for NUM in "${NUM_LIST[@]}"; do
    JSON_FILE="${NUM}${JSON_SUFFIX}"
    if [ ! -f "$JSON_FILE" ]; then
      echo "No ${JSON_FILE}"
    else
      # Send request
      response=$(
        curl -X 'POST' \
             "${CONTROLLER_ADDRESS}/schemas?override=true&force=false" \
             -H 'accept: application/json' \
             -H 'Content-Type: application/json' \
             -d @"${JSON_FILE}" \
             2>&1
      )
      echo "  Response for ${JSON_FILE}: ${response}"
    fi
  done
}

function add_table_to_pinot() {
  local JSON_SUFFIX=$1
  if [ -z "$CONTROLLER_ADDRESS" ]; then
    echo "No Controller address provided. Skipping table creation."
    return 0
  fi
  echo "  Adding table to ${CONTROLLER_ADDRESS}"

  for NUM in "${NUM_LIST[@]}"; do
    JSON_FILE="${NUM}${JSON_SUFFIX}"
    if [ ! -f "$JSON_FILE" ]; then
      echo "No ${JSON_FILE}"
    else
      # Send request
      response=$(
        curl -X 'POST' \
             "${CONTROLLER_ADDRESS}/tables" \
             -H 'accept: application/json' \
             -H 'Content-Type: application/json' \
             -d @"${JSON_FILE}" \
             2>&1
      )
      echo "  Response for ${JSON_FILE}: ${response}"
    fi
  done
}

# Main script
create_table_number_list

echo 'Schema'
generate_config $SCHEMA_SUFFIX
add_schema_to_pinot $SCHEMA_SUFFIX

echo 'Realtime'
generate_config $REALTIME_SUFFIX
add_table_to_pinot $REALTIME_SUFFIX

echo 'Offline'
generate_config $OFFLINE_SUFFIX
add_table_to_pinot $OFFLINE_SUFFIX
