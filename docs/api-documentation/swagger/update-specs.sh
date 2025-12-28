#!/bin/bash
echo "Exporting OpenAPI specs from running services..."
curl -s http://localhost:8082/v3/api-docs.yaml > docs/api-documentation/swagger/openapi-analytics.yaml
curl -s http://localhost:8083/v3/api-docs.yaml > docs/api-documentation/swagger/openapi-notification.yaml
echo "Done! Don't forget to commit the changes."