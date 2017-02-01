import { post } from 'redux-fetch'

import { getAllConfig } from '../../reducer'
import { clearWizard } from 'admin-wizard/actions'

export const changeStage = (stageId) => ({ type: 'SOURCE_CHANGE_STAGE', stage: stageId })
export const resetStages = () => ({type: 'SOURCE_RESET_STAGES'})
export const stagesClean = () => ({type: 'SOURCE_CLEAN_STAGES'})
export const stagesModified = () => ({type: 'SOURCE_MODIFIED_STAGES'})
export const setStageProgress = (stageId) => ({type: 'SET_CURRENT_PROGRESS', stage: stageId})
export const setNavStage = (stageId) => ({type: 'SOURCE_NAV_STAGE', stage: stageId})
export const setSourceSelections = (selections) => ({type: 'SET_SOURCE_SELECTIONS', sourceConfigs: selections})
export const setSelectedSource = (source) => ({type: 'SET_SELECTED_SOURCE', selectedSource: source})
export const clearConfiguration = () => ({type: 'CLEAR_CONFIG'})
export const setMessages = (id, messages) => ({ type: 'SET_MESSAGES', id, messages })
export const clearMessages = (id) => ({ type: 'CLEAR_MESSAGES', id })
export const startSubmitting = () => ({ type: 'START_SUBMITTING' })
export const endSubmitting = () => ({ type: 'END_SUBMITTING' })
export const setConfigSource = (source) => ({ type: 'SET_CONFIG_SOURCE', value: source })
export const setConfigTypes = (types) => ({ type: 'SOURCES/SET_CONFIG_IDS', types })

export const resetSourceWizardState = () => (dispatch) => {
  dispatch(clearWizard())
  dispatch(clearConfiguration())
}

const containsErrors = (json) => {
  return json.messages.some((message) => (message.type === 'FAILURE'))
}

const filterErrors = (messages) => {
  return messages.filter((message) => (message.type === 'FAILURE'))
}

export const testSources = (configType, nextStageId, id = 'sources') => async (dispatch, getState) => {
  dispatch(startSubmitting())
  try {
    dispatch(clearMessages(id))
    const body = JSON.stringify({ configurationType: configType, ...getAllConfig(getState()) })

    let res = await dispatch(post('/admin/beta/config/test/sources/valid-url', { body }))
    let json = await res.json()

    if (containsErrors(json)) {
      dispatch(setMessages(id, filterErrors(json.messages)))
      return
    }

    res = await dispatch(post('/admin/beta/config/probe/sources/discover-sources', { body }))
    json = await res.json()

    if (containsErrors(json)) {
      dispatch(setMessages(id, filterErrors(json.messages)))
      return
    }

    let sources = [ ...json.probeResults.discoveredSources ].map((source) => (source.config))

    // TODO: tbatie - We should change this trustedCertAuth to untrustedCertAuth or something better. this logic is ugly
    json.probeResults.discoveredSources.forEach((source, i) => {
      if (sources.message !== undefined && sources.message > 0) {
        source.messages.forEach((message) => {
          if (message.subType === 'UNTRUSTED_CA') {
            sources[i].trustedCertAuthority = false
          }
        })
      } else {
        sources[i].trustedCertAuthority = true
      }
    })

    dispatch(setSourceSelections(sources))
    dispatch(clearMessages(id))
    dispatch(changeStage(nextStageId))
  } finally {
    dispatch(endSubmitting())
  }
}

export const persistConfig = (url, config, nextStageId, configType, id = 'sources') => async (dispatch, getState) => {
  dispatch(startSubmitting())
  try {
    dispatch(clearMessages(id))
    const body = JSON.stringify({ configurationType: configType, ...getAllConfig(getState()) })

    let res = await dispatch(post(url, { body }))
    let json = await res.json()

    if (containsErrors(json)) {
      dispatch(setMessages(id, filterErrors(json.messages)))
      return
    }

    dispatch(changeStage(nextStageId))
  } catch (e) {
    console.log(e)
  } finally {
    dispatch(endSubmitting())
  }
}

export const fetchConfigTypes = (nextStageId, id = 'sources') => async (dispatch, getState) => {
  dispatch(startSubmitting())
  try {
    const body = { configurationType: 'sources', ...getAllConfig(getState()) }

    const res = await dispatch(post('/admin/beta/config/probe/sources/config-handlers', { body }))
    const json = await res.json()

    if (containsErrors(json)) {
      dispatch(setMessages(id, filterErrors(json.messages)))
      return
    }

    dispatch(setConfigTypes(json.probeResults.sourceConfigHandlers))
    if (nextStageId) {
      dispatch(changeStage(nextStageId))
    }
  } finally {
    dispatch(endSubmitting())
  }
}

export const testManualUrl = (endpointUrl, configType, nextStageId, id = 'sources') => async (dispatch, getState) => {
  dispatch(startSubmitting())
  try {
    const body = {
      configurationType: configType,
      endpointUrl: endpointUrl,
      ...getAllConfig(getState())
    }
    const url = '/admin/beta/config/probe/' + configType + '/retrieveConfiguration'

    const res = await dispatch(post(url, { body }))
    const json = await res.json()

    if (containsErrors(json)) {
      dispatch(setMessages(id, filterErrors(json.messages)))
      return
    }

    dispatch(setConfigSource(json.probeResults.retrieveConfiguration))
    dispatch(changeStage(nextStageId))
  } finally {
    dispatch(endSubmitting())
  }
}
