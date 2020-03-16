#!/bin/bash

cd "$(dirname $0)" || exit 1;

KEY_MATERIAL_DIR=.key_material
mkdir $KEY_MATERIAL_DIR
cd $KEY_MATERIAL_DIR || exit 1;

MASH_PK="mashery"
MASH_PKCS8_PWD=".mashery.pwd"
MASH_PK_PKCS8="mashery.pkcs8"
MASHERY_PUB="${MASH_PK}.pub"
MASHERY_AES="${MASH_PK}.aes"

LAMBDA_PK="sidecar"
LAMBDA_PKCS8_PWD=".${LAMBDA_PK}.pwd"
LAMBDA_PK_PKCS8="${LAMBDA_PK}.pkcs8"
LAMBDA_PUB="${LAMBDA_PK}.pub"
LAMBDA_AES="${LAMBDA_PK}.aes"

echo "1. Generating key material for Mashery..."
openssl genrsa -out $MASH_PK 2048
openssl rsa -in $MASH_PK -out $MASHERY_PUB -pubout -outform PEM
openssl rand -hex 16 | tr -d '\n' | tr -d '\r' > $MASH_PKCS8_PWD
openssl pkcs8 -topk8 -inform PEM -outform PEM -in $MASH_PK -out $MASH_PK_PKCS8 -passout file:$MASH_PKCS8_PWD
openssl rand -hex 16 | tr -d '\n' | tr -d '\r' > $MASHERY_AES

echo "2. Generating key material for Lambda..."
openssl genrsa -out $LAMBDA_PK 2048
openssl rsa -in $LAMBDA_PK -out $LAMBDA_PUB -pubout -outform PEM
openssl rand -hex 16 | tr -d '\n' | tr -d '\r' > $LAMBDA_AES
openssl rand -hex 16 | tr -d '\n' | tr -d '\r' > $LAMBDA_PKCS8_PWD
openssl pkcs8 -topk8 -inform PEM -outform PEM -in $LAMBDA_PK -out $LAMBDA_PK_PKCS8 -passout file:$LAMBDA_PKCS8_PWD

echo " ------------------------------- "
echo "Key material has been created."