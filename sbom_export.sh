#! /bin/sh

function create_export {
  curl --silent \
  --header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" \
  -X 'POST' --data "export_type=sbom" \
  "https://gitlab.com/api/v4/pipelines/$CI_PIPELINE_ID/dependency_list_exports" \
  | jq '.id'
}

function check_status {
  curl --silent \
    --header "PRIVATE-TOKEN: glpat-rZRZZWoTwJMEPAaFZCRh" \
    --write-out "%{http_code}" --output /dev/null \
    https://gitlab.com/api/v4/dependency_list_exports/$1
}

function download {
  curl --header "PRIVATE-TOKEN: glpat-rZRZZWoTwJMEPAaFZCRh" \
    --output "gl-sbom-merged-$CI_PIPELINE_ID.cdx.json" \
    "https://gitlab.com/api/v4/dependency_list_exports/$1/download"
}

function export_sbom {
  local ID=$(create_export)

  for run in $(seq 0 3); do
    local STATUS=$(check_status $ID)
    # Status is 200 when JSON is generated.
    # Status is 202 when generate JSON job is running.
    if [ $STATUS -eq "200" ]; then
      download $ID

      exit 0
    elif [ $STATUS -ne "202" ]; then
      exit 1
    fi

    echo "Waiting for JSON to be generated"
    sleep 5
  done

  exit 1
}