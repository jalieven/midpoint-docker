CREATE TABLE source_accounts (
    accountid               VARCHAR(36) NOT NULL,
    username                VARCHAR(64) NOT NULL,
    firstname               VARCHAR(64) NOT NULL,
    lastname                VARCHAR(64),
    rijksregisternummer     VARCHAR(11),
    disabled                BOOLEAN,
    PRIMARY KEY (accountId)
);

CREATE TABLE source_entitlements (
    entitlementid           VARCHAR(128) NOT NULL,
    accountid               VARCHAR(36) NOT NULL,
    email                   VARCHAR(128),
    organisatiecode         VARCHAR(128),
    departement             VARCHAR(64),
    dienst                  VARCHAR(64),
    functie                 VARCHAR(64),
    personeelsnummer        VARCHAR(32),
    fax                     VARCHAR(32),
    gsm                     VARCHAR(32),
    telefoonnr              VARCHAR(32),
    privileges              VARCHAR(128),
    disabled                BOOLEAN,
    PRIMARY KEY (entitlementid)
);

alter table source_entitlements add constraint c_source_entitlements_fk_source_accounts foreign key (accountid) references source_accounts;
