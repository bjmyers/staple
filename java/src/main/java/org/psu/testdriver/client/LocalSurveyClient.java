package org.psu.testdriver.client;

import org.psu.miningmanager.dto.ExtractResponse;
import org.psu.miningmanager.dto.Survey;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.api.SurveyClient;
import org.psu.spacetraders.dto.DataWrapper;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Local version of the {@link SurveyClient}
 */
@ApplicationScoped
public class LocalSurveyClient implements SurveyClient {

	@Override
	public DataWrapper<SurveyResponse> survey(String shipId) {
		return null;
	}

	@Override
	public DataWrapper<ExtractResponse> extractSurvey(String shipId, Survey survey) {
		return null;
	}

}
