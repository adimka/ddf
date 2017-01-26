import React from 'react'

import Mount from 'react-mount'

import Stage from 'components/Stage'
import Title from 'components/Title'
import Description from 'components/Description'
import Action from 'components/Action'
import ActionGroup from 'components/ActionGroup'
import Message from 'components/Message'
import Spinner from 'components/Spinner'

import {
  Input,
  Password,
  Select
} from 'admin-wizard/inputs'

const BindSettings = (props) => {
  const {
    // data
    disabled,
    submitting,
    configs = {},
    messages = [],

    // actions
    prev,
    test,
    setDefaults
  } = props

  const { bindUserMethod, encryptionMethod } = configs
  let bindUserMethodOptions = ['Simple']

  if (encryptionMethod === 'LDAPS' || encryptionMethod === 'StartTLS') {
    bindUserMethodOptions.push('Digest MD5 SASL')
  }

  return (
    <Stage>
      <Mount
        on={setDefaults}
        bindUserDn='cn=admin'
        bindUserPassword='secret'
        bindUserMethod='Simple' />

      <Spinner submitting={submitting}>
        <Title>LDAP Bind User Settings</Title>
        <Description>
          Now that we've figured out the network environment, we need to
          bind a user to the LDAP Store to retrieve additional information.
        </Description>

        <Input id='bindUserDn' disabled={disabled} label='Bind User DN' />
        <Password id='bindUserPassword' disabled={disabled} label='Bind User Password' />
        <Select id='bindUserMethod'
          label='Bind User Method'
          disabled={disabled}
          options={bindUserMethodOptions} />
        {/* removed options: 'SASL', 'GSSAPI SASL' */}
        {/* TODO GSSAPI SASL only */}
        {/* <Input id='bindKdcAddress' disabled={disabled} label='KDC Address (for Kerberos authentication)' /> */}
        {/* TODO GSSAPI and Digest MD5 SASL only */}
        {
          (bindUserMethod === 'Digest MD5 SASL')
            ? (<Input id='bindRealm' disabled={disabled} label='Realm (for Kerberos and Digest MD5 authentication)' />)
            : null
        }

        <ActionGroup>
          <Action
            secondary
            label='back'
            onClick={prev}
            disabled={disabled} />
          <Action
            primary
            label='next'
            onClick={test}
            disabled={disabled}
            nextStageId='directory-settings'
            testId='bind' />
        </ActionGroup>

        {messages.map((msg, i) => <Message key={i} {...msg} />)}
      </Spinner>
    </Stage>
  )
}

export default BindSettings

