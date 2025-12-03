# cropcareBackend/storage_backends.py
from django.conf import settings
from storages.backends.s3boto3 import S3Boto3Storage

class StaticStorage(S3Boto3Storage):
    """
    Storage para archivos estáticos (static/)
    - Para pruebas usamos public-read para que los archivos sean accesibles públicamente.
    """
    location = "static"
    default_acl = "public-read"
    bucket_name = settings.AWS_STORAGE_BUCKET_NAME


class MediaStorage(S3Boto3Storage):
    """
    Storage para media (media/)
    - Para pruebas optamos por public-read (facilita acceso desde Android).
    - file_overwrite = False evita sobreescrituras por nombre idéntico.
    """
    location = "media"
    file_overwrite = False
    default_acl = "public-read"
    bucket_name = settings.AWS_STORAGE_BUCKET_NAME
