/* eGov suite of products aim to improve the internal efficiency,transparency,
   accountability and the service delivery of the government  organizations.

    Copyright (C) <2015>  eGovernments Foundation

    The updated version of eGov suite of products as by eGovernments Foundation
    is available at http://www.egovernments.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/ or
    http://www.gnu.org/licenses/gpl.html .

    In addition to the terms of the GPL license to be adhered to in using this
    program, the following additional terms are to be complied with:

        1) All versions of this program, verbatim or modified must carry this
           Legal Notice.

        2) Any misrepresentation of the origin of the material is prohibited. It
           is required that all modified versions of this material be marked in
           reasonable ways as different from the original version.

        3) This license does not grant any rights to any user of the program
           with regards to rights under trademark law for use of the trade names
           or trademarks of eGovernments Foundation.

  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.adtax.web.controller.hoarding;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.egov.adtax.entity.Advertisement;
import org.egov.adtax.entity.AdvertisementPermitDetail;
import org.egov.adtax.exception.HoardingValidationError;
import org.egov.adtax.utils.constants.AdvertisementTaxConstants;
import org.egov.adtax.web.controller.common.HoardingControllerSupport;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hoarding")
public class UpdateLegacyAdvertisementController extends HoardingControllerSupport {

    @ModelAttribute("advertisement")
    public Advertisement advertisement(@PathVariable final String id) {
        return advertisementService.findBy(Long.valueOf(id));
    }

    @ModelAttribute("advertisementPermitDetail")
    public AdvertisementPermitDetail advertisementPermitDetail(@PathVariable final String id) {
        return advertisementPermitDetailService.findBy(Long.valueOf(id));
    }

    @RequestMapping(value = "/updateLegacy/{id}", method = GET)
    public String updateHoarding(@PathVariable final String id, final Model model) {  
        final Advertisement advertisement = advertisementService.findByAdvertisementNumber(id);
        model.addAttribute("advertisementPermitDetail", advertisement.getActiveAdvertisementPermit());
        model.addAttribute("advertisementDocuments", advertisement.getDocuments());
        return "hoarding-update"; 
    }

    @RequestMapping(value = "updateLegacy/{id}", method = POST)
    public String updateHoarding(@Valid @ModelAttribute final AdvertisementPermitDetail advertisementPermitDetail,
            final BindingResult resultBinder, final RedirectAttributes redirAttrib, final HttpServletRequest request,
            final Model model,
            @RequestParam String workFlowAction) {

        validateHoardingDocsOnUpdate(advertisementPermitDetail, resultBinder, redirAttrib);

        if (resultBinder.hasErrors())
            return "hoarding-update";
        try {
            
            updateHoardingDocuments(advertisementPermitDetail);
            advertisementPermitDetailService.updateAdvertisementPermitDetail(advertisementPermitDetail, null,
                    null, AdvertisementTaxConstants.CREATE_ADDITIONAL_RULE, workFlowAction);
            
            return "redirect:/hoarding/success/" + advertisementPermitDetail.getId();
        } catch (final HoardingValidationError e) {
            resultBinder.rejectValue(e.fieldName(), e.errorCode());
            return "hoarding-update";
        }
    }
}
