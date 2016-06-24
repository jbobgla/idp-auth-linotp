package edu.kit.scc.linotp;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ExtractTokenFromForm extends AbstractExtractionAction<SAMLObject, SAMLObject> {
	
	/** Class logger. */
	@Nonnull
	private final Logger logger = LoggerFactory.getLogger(ExtractTokenFromForm.class);

	@Nonnull
	@NotEmpty
	private String tokenCodeField;

	public ExtractTokenFromForm() {
		super();
	}

	public void setTokenCodeField(@Nonnull @NotEmpty final String fieldName) {
		logger.debug("{} {} is tokencode field from the form", getLogPrefix(), fieldName);
		ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
		tokenCodeField = fieldName;
	}


	@Override
	protected void doExecute(@Nonnull final ProfileRequestContext<SAMLObject, SAMLObject> profileRequestContext,
			@Nonnull final AuthenticationContext authenticationContext) {
		
		final HttpServletRequest request = getHttpServletRequest();

		if (request == null) {
			logger.debug("{} Empty HttpServletRequest", getLogPrefix());
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
			return;
		}

		try {

			TokenContext tokenCtx = authenticationContext.getSubcontext(TokenContext.class, true);

			/** get tokencode from request **/
			String value = StringSupport.trimOrNull(request.getParameter(tokenCodeField));

			if (Strings.isNullOrEmpty(value)) {
				logger.debug("{} Empty tokenCode", getLogPrefix());
				ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
				return;
			} else {
				logger.debug("{} TokenCode: {}", getLogPrefix(), value);

				/** set tokencode to TokenCodeContext **/
				tokenCtx.setToken(value);
				logger.debug("Put Token code to the TokenCodeCtx");
				return;
			}

		} catch (Exception e) {
			logger.warn("{} Login by {} produced exception", getLogPrefix(),  e);
		}		
	}
}
