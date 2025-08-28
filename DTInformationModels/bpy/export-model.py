import bpy
import mathutils
import os

output_path = os.path.join(os.getcwd(), 'model')
for object in bpy.context.scene.objects.values():
    bpy.ops.object.select_all(action='DESELECT')

    object.select_set(True)
    last = object.location.to_tuple()
    object.location = mathutils.Vector((0.0, 0.0, 0.0))

    bpy.ops.export_scene.gltf(filepath=os.path.join(output_path, object.name), use_selection=True, export_materials='EXPORT')#, export_image_format='WEBP')
    object.location = mathutils.Vector(last)
