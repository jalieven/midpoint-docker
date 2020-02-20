create table if not exists webidm_accounts (
    account_id varchar(36) not null,
    creation_date timestamp,
    last_modified_date timestamp,
    deleted boolean not null,
    enabled boolean not null,
    locked boolean not null,
    user_rijksregisternummer varchar(11) not null,
    primary key (account_id)
);

create table if not exists webidm_entitlement_privilege (
    entitlement_entitlement_id varchar(255) not null,
    privilege_name varchar(255) not null,
    primary key (entitlement_entitlement_id, privilege_name)
);

create table if not exists webidm_entitlements (
    entitlement_id varchar(255) not null,
    creation_date timestamp,
    last_modified_date timestamp,
    deleted boolean not null,
    department varchar(255),
    email varchar(255),
    employee_number varchar(255),
    fax varchar(64),
    function varchar(255),
    mobile varchar(64),
    organisation_code varchar(255),
    organisation_code_detail varchar(255),
    service varchar(255),
    telephone varchar(64),
    account_id varchar(36) not null,
    primary key (entitlement_id)
);

create table if not exists webidm_privileges (
    name varchar(255) not null,
    primary key (name)
);

create table if not exists webidm_users (
    rijksregisternummer varchar(11) not null,
    given_name varchar(255) not null,
    surname varchar(255) not null,
    primary key (rijksregisternummer)
);

alter table if exists webidm_accounts
    add constraint fk_user_rijksregisternummer
        foreign key (user_rijksregisternummer)
            references webidm_users;

alter table if exists webidm_entitlement_privilege
    add constraint fk_entitlement_id
        foreign key (entitlement_entitlement_id)
            references webidm_entitlements;

alter table if exists webidm_entitlement_privilege
    add constraint fk_privilege_name
        foreign key (privilege_name)
            references webidm_privileges;

alter table if exists webidm_entitlements
    add constraint fk_account_id
        foreign key (account_id)
            references webidm_accounts;
