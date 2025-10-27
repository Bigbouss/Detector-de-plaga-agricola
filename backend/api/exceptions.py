"""
api/exceptions.py
Formateo uniforme de errores DRF para respuestas consistentes.
"""

from rest_framework.views import exception_handler

def custom_exception_handler(exc, context):
    response = exception_handler(exc, context)
    if response is None:
        return None
    request = context.get("request")
    response.data = {
        "success": False,
        "status_code": response.status_code,
        "path": request.get_full_path() if request else None,
        "errors": response.data,
    }
    return response
