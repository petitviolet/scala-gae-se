BATCH_HOST=localhost
BATCH_PORT=4567

PROJECT_STAGING=petit-violet
PROJECT_PRODUCTION=petit-violet
PROJECT_TARGET=

MAIN_WEB_INF_DIR=./modules/main/src/main/webapp/WEB-INF
WEB_INF_DIR=

compile:  ## compils scala modules
	sbt -mem 2048 clean compile

version-update: ## show library update candidates
	sbt dependencyUpdates

check-project:
ifndef PROJECT_TARGET
	@echo 'you should provide `PROJECT_TARGET=<PROJECT_ID>`'
	@exit 1;
endif

check-web-inf:
ifndef WEB_INF_DIR
	@echo 'you should provide `WEB_INF_DIR=<WEB_INF_DIR>`'
	@exit 1;
endif

# 直接は使わない
gae/deploy: check-project check-web-inf ## deploy gae module to PROJECT_TARGET using WEB_INF_DIR
	rm -rf **/target
	gcloud config set project $(PROJECT_TARGET)
	@# バージョン番号を自動生成
	@# デプロイターゲットを引数で切り替え
	@# 環境変数をデプロイターゲットによって切り替える
	@cat $(WEB_INF_DIR)/appengine-web.xml.template | \
		sed -e "s/{{VERSION}}/$$(./version.sh)/g" | \
		sed -e "s/{{PROJECT}}/$(PROJECT_TARGET)/g" | \
		sed -e "s|{{ENV_VARIABLES}}|$$(sed ':loop; N; $$! b loop; s/\n/\\n\t\t/g' $(WEB_INF_DIR)/appengine-web-env.$(PROJECT_TARGET).xml)|g" > $(WEB_INF_DIR)/appengine-web.xml
	sbt clean compile test appengineDeploy

main/deploy/staging: ## deploy main module to staging env
	@make gae/deploy PROJECT_TARGET=$(PROJECT_STAGING) WEB_INF_DIR=$(MAIN_WEB_INF_DIR)

main/deploy/production: ## deploy main module to production env
	@make gae/deploy PROJECT_TARGET=$(PROJECT_PRODUCTION) WEB_INF_DIR=$(MAIN_WEB_INF_DIR)

help: ## show help
	@egrep -E '^.+:.*?## .*$$' Makefile | grep -v '@egrep' | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[0;36m%-30s\033[0;39m %s\n", $$1, $$2}'

