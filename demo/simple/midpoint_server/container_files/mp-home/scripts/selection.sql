-- SearchScript

SELECT
    acc.*,
    usr.*,
    string_agg(ent.entitlement_id, ';') as entitlements
FROM webidm_accounts acc
         LEFT JOIN webidm_entitlements ent
                   ON ent.account_id = acc.account_id
         LEFT JOIN webidm_users usr
                   ON usr.rijksregisternummer = acc.user_rijksregisternummer
WHERE acc.deleted = false AND ent.deleted = false
GROUP BY acc.account_id, usr.rijksregisternummer;

SELECT ent.*,
       string_agg(priv.name, ';' ORDER BY priv.name) as privileges
FROM webidm_entitlements ent
         LEFT JOIN webidm_entitlement_privilege ent_priv
                   ON ent_priv.entitlement_entitlement_id = ent.entitlement_id
         LEFT JOIN webidm_privileges priv
                   ON priv.name = ent_priv.privilege_name
WHERE ent.deleted = false
GROUP BY ent.entitlement_id;


-- SyncScript

SELECT
    acc.*,
    usr.*,
    string_agg(ent.entitlement_id, ';') as entitlements
FROM webidm_accounts acc
         LEFT JOIN webidm_entitlements ent
                   ON ent.account_id = acc.account_id
         LEFT JOIN webidm_users usr
                   ON usr.rijksregisternummer = acc.user_rijksregisternummer
WHERE acc.deleted = false AND ent.deleted = false AND acc.last_modified_date > TO_TIMESTAMP('2020-02-17 10:32:53.505000', 'YYYY-MM-DD HH24:MI:SS.US')
GROUP BY acc.account_id, usr.rijksregisternummer;

SELECT ent.*,
       string_agg(priv.name, ';' ORDER BY priv.name) as privileges
FROM webidm_entitlements ent
         LEFT JOIN webidm_entitlement_privilege ent_priv
                   ON ent_priv.entitlement_entitlement_id = ent.entitlement_id
         LEFT JOIN webidm_privileges priv
                   ON priv.name = ent_priv.privilege_name
WHERE ent.deleted = false AND ent.last_modified_date > TO_TIMESTAMP('2020-02-17 10:32:53.505000', 'YYYY-MM-DD HH24:MI:SS.US')
GROUP BY ent.entitlement_id;




SELECT MAX(last_modified_date) AS max_modification, LOCALTIMESTAMP AS now FROM webidm_accounts;

SELECT MAX(last_modified_date) AS max_modification, LOCALTIMESTAMP AS now FROM webidm_entitlements;