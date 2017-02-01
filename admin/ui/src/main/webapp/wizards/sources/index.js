import React from 'react'
import { connect } from 'react-redux'
import { getSourceStage, getIsSubmitting } from './reducer'
import { getMessages } from '../../reducer'

import Flexbox from 'flexbox-react'
import CircularProgress from 'material-ui/CircularProgress'
import Paper from 'material-ui/Paper'

import styles from './styles.less'

import Mount from 'react-mount'
import { clearWizard } from 'admin-wizard/actions'

import {
  WelcomeStage,
  DiscoveryStage,
  SourceSelectionStage,
  ConfirmationStage,
  CompletedStage,
  ManualEntryStage
} from './stages'

const WizardView = ({ id, children, clearWizard }) => (
  <Mount key={id}>{children}</Mount>
)

const Wizard = connect(null, { clearWizard })(WizardView)

/*
  - welcomeStage
  - discoveryStage
  - sourceSelectionStage
  - confirmationStage
  - completedStage
  - manualEntryStage
*/

let StageRouter = ({ stage, messages }) => {
  const stageMapping = {
    welcomeStage: <WelcomeStage messages={messages} />,
    discoveryStage: <DiscoveryStage messages={messages} />,
    sourceSelectionStage: <SourceSelectionStage messages={messages} />,
    confirmationStage: <ConfirmationStage messages={messages} />,
    completedStage: <CompletedStage messages={messages} />,
    manualEntryStage: <ManualEntryStage messages={messages} />
  }
  return (stageMapping[stage])
}
StageRouter = connect((state) => ({
  stage: getSourceStage(state),
  messages: getMessages(state, 'sources')
}))(StageRouter)

let SourceApp = ({ isSubmitting = false, value = {}, setDefaults, messages }) => (
  <Wizard id='sources'>
    <Paper className={styles.main}>
      {isSubmitting
        ? <div className={styles.submitting}>
          <Flexbox justifyContent='center' alignItems='center' width='100%'>
            <CircularProgress size={60} thickness={7} />
          </Flexbox>
        </div>
        : null}
      <StageRouter />
    </Paper>
  </Wizard>
)
SourceApp = connect((state) => ({
  isSubmitting: getIsSubmitting(state)
}))(SourceApp)

export default SourceApp
