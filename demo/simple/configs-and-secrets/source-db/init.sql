CREATE TABLE source_accounts (
    accountid               VARCHAR(36) NOT NULL,
    username                VARCHAR(32) NOT NULL,
    firstname               VARCHAR(64) NOT NULL,
    lastname                VARCHAR(64),
    rijksregisternummer     VARCHAR(11),
    disabled                BOOLEAN,
    PRIMARY KEY (accountId)
);

CREATE TABLE source_entitlements (
    entitlementid           VARCHAR(128) NOT NULL,
    accountid               VARCHAR(36) NOT NULL,
    email                   VARCHAR(32),
    organisatiecode         VARCHAR(32),
    departement             VARCHAR(32),
    dienst                  VARCHAR(32),
    functie                 VARCHAR(32),
    personeelsnummer        VARCHAR(32),
    fax                     VARCHAR(32),
    gsm                     VARCHAR(32),
    telefoonnr              VARCHAR(32),
    privileges              VARCHAR(32),
    disabled                BOOLEAN,
    PRIMARY KEY (entitlementid)
);

alter table source_entitlements add constraint c_source_entitlements_fk_source_accounts foreign key (accountid) references source_accounts;

INSERT INTO public.source_accounts(
	accountId, username, firstname, lastname, rijksregisternummer, disabled)
	VALUES ('3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jalie', 'Jan', 'Lievens', '81071040575', false);

INSERT INTO public.source_accounts(
	accountId, username, firstname, lastname, rijksregisternummer, disabled)
	VALUES ('ee6a51b5-7ef6-40eb-ac1b-54172a3ed520', 'tvgulck', 'Tom', 'Van Gulck', '78072240377', false);

-- Enable this when testing multiple accounts in a resource
-- INSERT INTO public.source_accounts(
-- 	accountId, username, firstname, lastname, rijksregisternummer, disabled)
-- 	VALUES ('a26ef559-fbbd-4a3d-8122-99a70184ba36', 'jalie', 'Jan', 'Lievens', '81071040575', false);

INSERT INTO public.source_entitlements(
	entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled)
	VALUES ('20000-Burger-01', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@biggerfish.be', '20000', 'HR', 'Thuis', 'CEO', NULL, NULL, '0494846697', '053839066', 'IMJVPlichtige', false);

INSERT INTO public.source_entitlements(
	entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled)
	VALUES ('20001-Milieumedewerker-01', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@vlaanderen.be', '20001', 'Omgeving', 'DIDM', 'Developer', NULL, NULL, '0494846697', '053839066', 'Milieumedewerker;Developer;VIP', false);
