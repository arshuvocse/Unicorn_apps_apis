-- SUPERSEDED for sp_Webapi_Save_DcrInfo / sp_Webapi_Get_ChamberName: re-run dcr_area_lock_and_tourplan_gate.sql after this script, or the DCR area lock / tour plan gate is lost.
-- Extends the DCR offline-sync source (sp_Webapi_Get_ChamberName, called per employee at
-- app sync time and cached into the device's local tblDoctor_ChembarName table) so Shift,
-- SittingTime, Address and Institution are available offline in AddDCRActivity without a
-- live network call. Depends on the Shift/SittingTime/InstitutionId columns added by
-- update_doctor_chamber_institution.sql (run that first).
ALTER PROCEDURE [dbo].[sp_Webapi_Get_ChamberName]
	@empid INT
AS
BEGIN
	SELECT DISTINCT
		tblDoctorChemberDetail.ChemberId ChemberId,
		ISNULL(tblDoctorChemberDetail.Name,'N/A') ChemberName,
		tblDoctorChemberDetail.DoctorId,
		ISNULL(tblDoctorChemberDetail.Shift,'') Shift,
		ISNULL(tblDoctorChemberDetail.SittingTime,'') SittingTime,
		ISNULL(tblDoctorChemberDetail.Address,'') Address,
		tblDoctorChemberDetail.InstitutionId,
		ISNULL(inst.Institution,'') InstitutionName
	FROM dbo.tblDoctorChemberDetail with (nolock)
	inner JOIN dbo.View_DoctorMaster CV  with (nolock) ON CV.DoctorId=tblDoctorChemberDetail.DoctorId
	inner JOIN dbo.tblDoctorMaster doc  with (nolock) ON doc.DoctorId=tblDoctorChemberDetail.DoctorId
	left JOIN dbo.tblInstitutionInfo inst with (nolock) ON inst.InstitutionId=tblDoctorChemberDetail.InstitutionId
	where doc.ApprovalStatus='2' and (CV.NSMEmpInfoId=@empid OR CV.RSMEmpInfoId=@empid OR CV.ASMEmpInfoId=@empid OR CV.MIOEmpInfoId=@empid)
END
GO
