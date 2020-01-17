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

-- Enable this when testing multiple accounts in a resource
-- INSERT INTO public.source_accounts(
-- 	accountId, username, firstname, lastname, rijksregisternummer, disabled)
-- 	VALUES ('a26ef559-fbbd-4a3d-8122-99a70184ba36', 'jalie', 'Jan', 'Lievens', '81071040575', false);

INSERT INTO public.source_accounts(
	accountId, username, firstname, lastname, rijksregisternummer, disabled)
	VALUES ('ee6a51b5-7ef6-40eb-ac1b-54172a3ed520', 'tvgulck', 'Tom', 'Van Gulck', '78072240377', false);

-- INSERT INTO public.source_accounts(
-- 	accountId, username, firstname, lastname, rijksregisternummer, disabled)
-- 	VALUES ('289b2860-510e-4ce7-8b01-0213936dd01a', 'jbouck', 'Joost', 'Bouckenooghe', '81062140279', false);

INSERT INTO public.source_entitlements(
	entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled)
	VALUES ('20000-Burger-01', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@biggerfish.be', '20000', 'HR', 'Thuis', 'CEO', NULL, NULL, '0494846697', '053839066', 'IMJVPlichtige;VIP', false);

-- INSERT INTO public.source_entitlements(
-- 	entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled)
-- 	VALUES ('20001-Milieumedewerker-01', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@vlaanderen.be', '20001', 'Omgeving', 'DIDM', 'Developer', NULL, NULL, '0494846697', '053839066', 'Milieumedewerker;Developer;VIP', false);

-- INSERT INTO public.source_entitlements(
-- 	entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled)
-- 	VALUES ('20001-Milieumedewerker-02', '289b2860-510e-4ce7-8b01-0213936dd01a', 'joost@mileuinfo.be', '20001', 'Omgeving', 'DIDM', 'Ops', NULL, NULL, '0494844342', '053849083', 'Milieumedewerker;Ops', false);

-- ###############
-- SYNC SCENARIOS:
-- ###############

-- 1) What happens when an existing coupled entitlement is recoupled with a different account? (This is in fact not a common use-case but we see lingering uniqueMembers in LDAP)
-- UPDATE public.source_entitlements
-- 	SET accountid = 'ee6a51b5-7ef6-40eb-ac1b-54172a3ed520'
-- 	WHERE entitlementid = '20001-Milieumedewerker-01';

-- 2) What happens when the lastname changes of an account?
-- UPDATE public.source_accounts
-- 	SET lastname = 'Hofman'
-- 	WHERE accountId = '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95';

-- 3) What happens when an entitlement gets added to an account
-- INSERT INTO public.source_entitlements(
-- 	entitlementid, accountid, email, organisatiecode, departement, dienst, functie, personeelsnummer, fax, gsm, telefoonnr, privileges, disabled)
-- 	VALUES ('20001-Milieumedewerker-03', '3fd83cd4-d1bb-4d7f-9a1a-12a02ed85a95', 'jan.lievens@gmail.com', '42301', 'Metingen', 'Telemetrie', 'Meet deskundige', NULL, NULL, '0494844342', '053849083', 'Milieucoordinator', false);

-- 4) What happens when an existing coupled entitlement is deleted?
-- DELETE FROM public.source_entitlements
-- 	WHERE entitlementid = '20001-Milieumedewerker-03';

-- 5) What happens when we delete a user?
